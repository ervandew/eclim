/*

  Copyright 2004, Martian Software, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/

package com.martiansoftware.nailgun;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.ExitException;

/**
 * Reads the NailGun stream from the client through the command,
 * then hands off processing to the appropriate class.  The NGSession
 * obtains its sockets from an NGSessionPool, which created this
 * NGSession.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class NGSession extends Thread {

  /**
   * The server this NGSession is working for
   */
  private NGServer server = null;

  /**
   * The pool this NGSession came from, and to which it will
   * return itself
   */
  private NGSessionPool sessionPool = null;

  /**
   * Synchronization object
   */
  private Object lock = new Object();

  /**
   * The next socket this NGSession has been tasked with processing
   * (by NGServer)
   */
  private Socket nextSocket = null;

  /**
   * True if the server has been shutdown and this NGSession should
   * terminate completely
   */
  private boolean done = false;

  /**
   * The instance number of this NGSession.  That is, if this is the Nth
   * NGSession to be created, then this is the value for N.
   */
  private long instanceNumber = 0;

  /**
   * A lock shared among all NGSessions
   */
  private static Object sharedLock = new Object();

  /**
   * The instance counter shared among all NGSessions
   */
  private static long instanceCounter = 0;

  /**
   * signature of main(String[]) for reflection operations
   */
  private static Class[] mainSignature;

  /**
   * signature of nailMain(NGContext) for reflection operations
   */
  private static Class[] nailMainSignature;

  static {
    // initialize the signatures
    mainSignature = new Class[1];
    mainSignature[0] = String[].class;

    nailMainSignature = new Class[1];
    nailMainSignature[0] = NGContext.class;
  }

  /**
   * Creates a new NGSession running for the specified NGSessionPool and
   * NGServer.
   * @param sessionPool The NGSessionPool we're working for
   * @param server The NGServer we're working for
   */
  NGSession(NGSessionPool sessionPool, NGServer server) {
    super();
    this.sessionPool = sessionPool;
    this.server = server;

    synchronized(sharedLock) {
      this.instanceNumber = ++instanceCounter;
    }
//    server.out.println("Created NGSession " + instanceNumber);
  }

  /**
   * Shuts down this NGSession gracefully
   */
  void shutdown() {
    done = true;
    synchronized(lock) {
      nextSocket = null;
      lock.notifyAll();
    }
  }

  /**
   * Instructs this NGSession to process the specified socket, after which
   * this NGSession will return itself to the pool from which it came.
   * @param socket the socket (connected to a client) to process
   */
  public void run(Socket socket) {
    synchronized(lock) {
      nextSocket = socket;
      lock.notify();
    }
    Thread.yield();
  }

  /**
   * Returns the next socket to process.  This will block the NGSession
   * thread until there's a socket to process or the NGSession has been
   * shut down.
   *
   * @return the next socket to process, or <code>null</code> if the NGSession
   * has been shut down.
   */
  private Socket nextSocket() {
    Socket result = null;
    synchronized(lock) {
      result = nextSocket;
      while (!done && result == null) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          done = true;
        }
        result = nextSocket;
      }
      nextSocket = null;
    }
    return (result);
  }

  /**
   * The main NGSession loop.  This gets the next socket to process, runs
   * the nail for the socket, and loops until shut down.
   */
  public void run() {

    updateThreadName(null);

    Socket socket = nextSocket();
    while (socket != null) {
      // EV: added by Anton for persistant connections
      boolean keepAlive = false;
      boolean byeChunk = false;
      // END CHANGE

      // EV: defining streams here so they can be cleaned up in finally block.
      java.io.DataInputStream sockin = null;
      java.io.OutputStream sockout = null;
      // END CHANGE
      try {
        // buffer for reading headers
        byte[] lbuf = new byte[5];

        // EV: defined outside of try to permit cleanup in finally.
        sockin = new java.io.DataInputStream(socket.getInputStream());
        sockout = socket.getOutputStream();
        // END CHANGE

        // client info - command line arguments and environment
        List remoteArgs = new java.util.ArrayList();
        Properties remoteEnv = new Properties();

        String cwd = null;      // working directory
        String command = null;    // alias or class name

        // read everything from the client up to and including the command
        // EV: change by Anton for persistant connections
        //while (command == null) {
        while (command == null && !byeChunk) {
        // END CHANGE
          sockin.readFully(lbuf);
          long bytesToRead = LongUtils.fromArray(lbuf, 0);
          char chunkType = (char) lbuf[4];

          byte[] b = new byte[(int) bytesToRead];
          sockin.readFully(b);
          String line = new String(b, "UTF-8");

          switch(chunkType) {

            case NGConstants.CHUNKTYPE_ARGUMENT:
                  //  command line argument
                  remoteArgs.add(line);
                  break;

            case NGConstants.CHUNKTYPE_ENVIRONMENT:
                  //  parse environment into property
                  int equalsIndex = line.indexOf('=');
                  if (equalsIndex > 0) {
                    remoteEnv.setProperty(
                        line.substring(0, equalsIndex),
                        line.substring(equalsIndex + 1));
                  }
                  break;

            case NGConstants.CHUNKTYPE_COMMAND:
                  //  command (alias or classname)
                  command = line;
                  break;

            case NGConstants.CHUNKTYPE_WORKINGDIRECTORY:
                  //  client working directory
                  cwd = line;
                  break;

            // EV: added by Anton for persistant connections
            case NGConstants.CHUNKTYPE_KEEP_ALIVE:
                  // allow the client to specify each time if the socket should
                  // be kept alive (open).  If server receives this chunk then
                  // the client session socket is left open and waits for next
                  // request (default is still to close the connection after
                  // each request, this allows old clients to operate normally
                  // as usual
                  keepAlive = true;
                  break;

            case NGConstants.CHUNKTYPE_BYE:
                  // Client can use optional "bye" chunk. It tells server to
                  // close the connection after the sequence of "keep alive"
                  // commands (alternatively client can: [a] close clients
                  // socket or [b] send one dummy single request command).
                  byeChunk = true;
                  keepAlive = false;
                  break;
            // END CHANGE

            default:  // freakout?
          }
        }

        updateThreadName(socket.getInetAddress().getHostAddress() + ": " + command);

        // can't create NGInputStream until we've received a command, because at
        // that point the stream from the client will only include stdin and stdin-eof
        // chunks
        InputStream in = new NGInputStream(sockin);
        PrintStream out = new PrintStream(new NGOutputStream(sockout, NGConstants.CHUNKTYPE_STDOUT));
        PrintStream err = new PrintStream(new NGOutputStream(sockout, NGConstants.CHUNKTYPE_STDERR));
        PrintStream exit = new PrintStream(new NGOutputStream(sockout, NGConstants.CHUNKTYPE_EXIT));

        // ThreadLocal streams for System.in/out/err redirection
        if (server.hasCapturedSystemStreams()){
          ((ThreadLocalInputStream) System.in).init(in);
          ((ThreadLocalPrintStream) System.out).init(out);
          ((ThreadLocalPrintStream) System.err).init(err);
        }

        // EV: if condition added by Anton for persistant connections.
        if (!byeChunk){
          try {
            Alias alias = server.getAliasManager().getAlias(command);
            Class cmdclass = null;
            if (alias != null) {
              cmdclass = alias.getAliasedClass();
            } else if (server.allowsNailsByClassName()) {
              // EV: use configurable classloader from server
              //cmdclass = Class.forName(command);
              cmdclass = Class.forName(command, true, server.getClassLoader());
            } else {
              cmdclass = server.getDefaultNailClass();
            }

            Object[] methodArgs = new Object[1];
            Method mainMethod = null; // will be either main(String[]) or nailMain(NGContext)
            String[] cmdlineArgs = (String[]) remoteArgs.toArray(new String[remoteArgs.size()]);

            try {
              mainMethod = cmdclass.getMethod("nailMain", nailMainSignature);
              NGContext context = new NGContext();
              context.setArgs(cmdlineArgs);
              context.in = in;
              context.out = out;
              context.err = err;
              context.setCommand(command);
              context.setExitStream(exit);
              context.setNGServer(server);
              context.setEnv(remoteEnv);
              context.setInetAddress(socket.getInetAddress());
              context.setPort(socket.getPort());
              context.setWorkingDirectory(cwd);
              methodArgs[0] = context;
            } catch (NoSuchMethodException toDiscard) {
              // that's ok - we'll just try main(String[]) next.
            }

            if (mainMethod == null) {
              mainMethod = cmdclass.getMethod("main", mainSignature);
              methodArgs[0] = cmdlineArgs;
            }

            if (mainMethod != null) {
              server.nailStarted(cmdclass);
                          NGSecurityManager.setExit(exit);

              try {
                mainMethod.invoke(null, methodArgs);
              } catch (InvocationTargetException ite) {
                throw(ite.getCause());
              } catch (Throwable t) {
                throw(t);
              } finally {
                server.nailFinished(cmdclass);
              }
              // EV: change by Anton
              //exit.println(0);
              exit.print(0);
            }

          } catch (ExitException exitEx) {
            // EV: change by Anton
            //exit.println(exitEx.getStatus());
            exit.print(exitEx.getStatus());

            server.out.println(Thread.currentThread().getName() + " exited with status " + exitEx.getStatus());
          } catch (Throwable t) {
            t.printStackTrace();
            // EV: change by Anton
            //exit.println(NGConstants.EXIT_EXCEPTION); // remote exception constant
            exit.print(NGConstants.EXIT_EXCEPTION); // remote exception constant
          }
        }

        // EV: moved to finally clause
        //socket.close();

      } catch (Throwable t) {
        t.printStackTrace();
        break;

      // EV: adding a finally clause to cleanup properly.
      } finally {
        if (!keepAlive) {

          // EV: handle odd case where, when executed from vim, extra byte(s)
          // are sent (a '.' being the culprit so far), which if unread, will
          // result in a ECONNRESET on the client side.
          try{
            byte[] leftovers = new byte[8];
            while(sockin.read(leftovers) > 0){
              // noop
            }
          }catch(Exception ignore){
          }

          try{
            sockout.close();
          }catch(Exception ignore){
          }
          try{
            sockin.close();
          }catch(Exception ignore){
          }

          try{
            socket.close();
          }catch(Exception ex){
            ex.printStackTrace();
          }
        }
      }

// EV: CHANGE
/* OLD
      ((ThreadLocalInputStream) System.in).init(null);
      ((ThreadLocalPrintStream) System.out).init(null);
      ((ThreadLocalPrintStream) System.err).init(null);
// NEW */
      // EV: if condition added by Anton for persistant connections.
      if (!keepAlive) {
        if (server.hasCapturedSystemStreams()) {
          if(System.in instanceof ThreadLocalInputStream){
            ((ThreadLocalInputStream) System.in).init(null);
          }
          if(System.out instanceof ThreadLocalPrintStream){
            ((ThreadLocalPrintStream) System.out).init(null);
          }
          if(System.out instanceof ThreadLocalPrintStream){
            ((ThreadLocalPrintStream) System.err).init(null);
          }
        }
// END CHANGE

        updateThreadName(null);
        sessionPool.give(this);
        socket = nextSocket();
      }
    }

//    server.out.println("Shutdown NGSession " + instanceNumber);
  }

  /**
   * Updates the current thread name (useful for debugging).
   */
  private void updateThreadName(String detail) {
    setName("NGSession " + instanceNumber + ": " + ((detail == null) ? "(idle)" : detail));
  }
}
