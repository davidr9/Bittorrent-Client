JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Bencoder2.java \
        BencodingException.java \
        TorrentInfo.java \
        ToolKit.java \
        RUBTClient.java \


default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class
