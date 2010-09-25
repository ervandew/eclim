import os, re

def test1():
  pass

def test2(foo, bar='baz'):
  pass

def test3(foo, bar='baz', *args, **kwargs):
  pass

__all__ = ('test1', 'test2', 'test3')
