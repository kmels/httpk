In [1]: f = lambda x,y: x+y #una funcion que suma sus dos argumentos
In [2]: l = [1,2,3,4,5]
In [3]: reduce(f,l)
Out[3]: 15
In [4]: g = lambda x,y: str(x)+str(y) #una funcion que contatena dos strings
In [5]: reduce(g,l)
Out[5]: '12345'
In [6]: h = lambda x,y: x*y #una funcion que multiplica sus dos argumentos
In [7]: reduce(h,l)
Out[7]: 120
