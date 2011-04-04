"""
Copyright (C) 2005 - 2011  Eric Van Dewoestine

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

@author: Anton Sharonov
@author: Eric Van Dewoestine
"""
import socket

try:
  from cStringIO import StringIO
except:
  from StringIO import StringIO

class Nailgun(object):
  """
  Client used to communicate with a nailgun server.
  """

  def __init__(self, **kwargs):
    self.socket = None
    self.port = kwargs.get('port')
    self.keepAlive = int(kwargs.get('keepAlive', 0))
    self.reconnectCounter = 0

  def send(self, cmdline):
    """
    Sends a complete command to the nailgun server.  Handles connecting to the
    server if not currently connected.
    @param cmdline command, which is sent to server, for instance
      "-command ping".
    @return tuple consisting of:
      - retcode from server (0 for success, non-0 for failure)
      - string response from server
    """
    if not self.isConnected():
      # with keepAlive do only first reconnect
      if not self.keepAlive or self.reconnectCounter == 0:
        (retcode, result) = self.reconnect()
        if retcode:
          return (retcode, result)

    if not self.isConnected(): # Only for keepAlive
      return (-1, "connect: ERROR - socket is not connected (nailgun.py)")

    try: # outer try for pre python 2.5 support.
      try:
        for arg in self.parseArgs(cmdline):
          self.sendChunk("A", arg)

        if self.keepAlive:
          self.sendChunk("K")

        self.sendChunk("C", "org.eclim.command.Main")

        (retcode, result) = self.processResponse()
        if self.keepAlive and retcode:
          # force reconnect on error (may not be necessary)
          self.reconnect()

        return (retcode, result)
      except socket.error, ex:
        args = ex.args
        if len(args) > 1:
          retcode, msg = args[0], args[1]
        elif len(args):
          retcode, msg = 1, args[0]
        else:
          retcode, msg = 1, 'No message'
        return (retcode, 'send: %s' % msg)
    finally:
      if not self.keepAlive:
        try:
          self.close()
        except:
          # don't let an error on close mask any previous error.
          pass

  def connect(self, port=None):
    """
    Establishes the connection to specified port or if not supplied,
    uses the default.
    """
    port = port or self.port
    try:
      sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      sock.connect(('localhost', port))
    except socket.error, ex:
      args = ex.args
      if len(args) > 1:
        retcode, msg = args[0], args[1]
      elif len(args):
        retcode, msg = 1, args[0]
      else:
        retcode, msg = 1, 'No message'
      return (retcode, 'connect: %s' % msg)

    self.socket = sock
    return (0, '')

  def reconnect(self):
    if self.socket != None:
      self.close()
    self.reconnectCounter += 1
    return self.connect()

  def close(self):
    self.socket.close()
    self.socket = None

  def isConnected(self):
    return self.socket != None

  def parseArgs(self, cmdline):
    # FIXME: doesn't handle escaping of spaces/quotes yet (may never need to)
    args = []
    arg = ''
    quote = ''
    for char in cmdline:
      if char == ' ' and not quote:
        if arg:
          args.append(arg)
          arg = ''
      elif char == '"' or char == "'":
        if quote and char == quote:
          quote = ''
        elif not quote:
          quote = char
        else:
          arg += char
      else:
        arg += char

    if arg:
      args.append(arg)

    return args

  def sendChunk(self, chunkType, text=''):
    """
    Sends a nailgun 'chunk' to the server.
    """
    #print("sendChunk " + chunkType + " " + text)
    length = len(text)
    str = "%c%c%c%c%c" % (
        (length / (65536*256)) % 256,
        (length / 65536) % 256,
        (length / 256) % 256,
        length % 256,
        chunkType)
    nbytes = self.socket.sendall(str)
    nbytes = self.socket.sendall(text)

  def processResponse(self):
    result = StringIO()
    exit = 0
    exitFlag = 1 # expecting 1 times exit chunk
    while exitFlag > 0:
      answer = self.recvBlocked(5)
      if len(answer) < 5:
        print("error: socket closed unexpectedly\n")
        return None
      lenPayload = ord(answer[0]) * 65536 * 256 \
        + ord(answer[1]) * 65536 \
        + ord(answer[2]) * 256 \
        + ord(answer[3])
      #print("lenPayload detected : %d" % lenPayload)
      chunkType = answer[4]
      if chunkType == "1":
        # STDOUT
        result.write(self.recvToFD(1, answer, lenPayload))
      elif chunkType == "2":
        # STDERR
        result.write(self.recvToFD(2, answer, lenPayload))
      elif chunkType == "X":
        exitFlag = exitFlag - 1
        exit = int(self.recvToFD(2, answer, lenPayload))
      else:
        print("error: unknown chunk type = %d\n" % chunkType)
        exitFlag = 0

    return [exit, result.getvalue()]

  def recvBlocked(self, lenPayload):
    """
    Receives until all data is read - necessary because usual recv sometimes
    returns with number of bytes read less then asked.
    """
    received = ""
    while (len(received) < lenPayload):
      received = received + self.socket.recv(lenPayload - len(received))
    return received

  def recvToFD(self, destFD, buf, lenPayload):
    """
    This function just mimics the function with the same name from the C
    client.  We don't really care which file descriptor the server tells us to
    write to - STDOUT and STDERR are the same on VIM side (see eclim.bat,
    "2>&1" at the end of command).
    """
    received = self.recvBlocked(lenPayload)
    return received
