cd "D:\work\ansj1.4\ansj_seg-master"
mvn assembly:assembly
java  -Dfile.encoding=utf-8  -cp target/ansj_seg-2.0.1-jar-with-dependencies.jar  cn.aolong.ldxz.AnsjServer