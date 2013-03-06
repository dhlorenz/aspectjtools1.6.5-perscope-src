copy /Y C:\aspectj_src\bin\org\aspectj\ajdt\internal\compiler\ast\*.class C:\aspectjtools\aspectjtools\org\aspectj\ajdt\internal\compiler\ast
copy /Y C:\aspectj_src\bin\org\aspectj\ajdt\internal\compiler\ast\perscope\*.class C:\aspectjtools\aspectjtools\org\aspectj\ajdt\internal\compiler\ast\perscope
copy /Y C:\aspectj_src\bin\org\aspectj\ajdt\internal\compiler\ast\perscope\storage\*.class C:\aspectjtools\aspectjtools\org\aspectj\ajdt\internal\compiler\ast\perscope\storage
copy /Y C:\aspectj_src\bin\org\aspectj\weaver\*.class C:\aspectjtools\aspectjtools\org\aspectj\weaver
copy /Y C:\aspectj_src\bin\org\aspectj\org\eclipse\jdt\internal\compiler\parser\Parser.class C:\aspectjtools\aspectjtools\org\aspectj\org\eclipse\jdt\internal\compiler\parser
copy /Y C:\aspectj_src\bin\org\aspectj\org\eclipse\jdt\internal\compiler\parser\TheOriginalJDTParserClass*.class C:\aspectjtools\aspectjtools\org\aspectj\org\eclipse\jdt\internal\compiler\parser
copy /Y C:\aspectj_src\bin\org\aspectj\weaver\patterns\*.class C:\aspectjtools\aspectjtools\org\aspectj\weaver\patterns

del /Q C:\Users\yulia\workspace\MyAspects\bin\my
"C:\Program Files (x86)\Java\jre6\bin\java" -Xdebug -Xrunjdwp:transport=dt_socket,address=8123,server=y,suspend=n -cp . org.aspectj.tools.ajbrowser.Main c:\lst\my.lst
