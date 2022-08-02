@echo off
rem SCOPUS Search
chcp 949
setLocal EnableDelayedExpansion
 
set CLASSPATH="
for /R ./lib %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)

set CLASSPATH=!CLASSPATH!"
 
java -server -Xms2g -Xmx2g -classpath "%CLASSPATH%" com.diquest.scopus.searcher._2021.external.ScopusSearcher_2021

pause