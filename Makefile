make:
	javac *.java

server:
	java StadiumServer

start:
	java StartStadiumCounter

clean:
	rm -rf *.class
