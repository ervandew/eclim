int link1(int n) {
  return n + 1;
}

int link2(int n) {
  return link1(n);
}

int link3(int n) {
  return link1(n) + link2(n);
}
