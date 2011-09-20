In [1]: f = lambda x: x + 5 #una funcion que le suma 5 a su argumento 
In [2]: l = [1,2,3,4,5]
In [3]: map(f,l)
Out[3]: [6, 7, 8, 9, 10] 
In [4]: f2 = lambda x: x * 2.0 #una funcion que multiplica por 2.0 a su argumento
In [5]: map(f2,l)
Out[5]: [2.0, 4.0, 6.0, 8.0, 10.0]
In [6]: f3 = lambda x: x % 2 #una funcion mod 2 
In [7]: map(f3,map(f2,l)) 
Out[7]: [0.0, 0.0, 0.0, 0.0, 0.0]
In [8]: map(f3,map(f,l))
Out[8]: [0, 1, 0, 1, 0]
