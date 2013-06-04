mvn -Pwin32 clean compile
mvn -Pwin32 assembly:single
cp target/ChiBE.jar ChiBE_win32.jar

mvn -Pwin64 clean compile
mvn -Pwin64 assembly:single
cp target/ChiBE.jar ChiBE_win64.jar

mvn -Plin32 clean compile
mvn -Plin32 assembly:single
cp target/ChiBE.jar ChiBE_linux32.jar

mvn -Plin64 clean compile
mvn -Plin64 assembly:single
cp target/ChiBE.jar ChiBE_linux64.jar

mvn -Pmac clean compile
mvn -Pmac assembly:single
cp target/ChiBE.jar ChiBE_macosx.jar
