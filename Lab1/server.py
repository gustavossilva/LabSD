import socket

s = socket.socket()
host = "localhost"
port = 12346
s.bind((host,port))
s.listen(5)

c,addr = s.accept()
print 'Got connection from', addr

while True:
	print c.recv(1024)
	str = raw_input("Digite sua resposta: ")
	c.send(str)

c.close()
s.close()
