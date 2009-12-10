int fun2(int n) {
  return n + 1;
}

int fun3(int n) {
  fun2(n);
  return fun1(n) + fun2(n);
}
