FROM raspbian/stretch
RUN sudo apt-get update && sudo apt-get -y upgrade && apt-get -y dist-upgrade
RUN sudo apt install -y default-jdk
RUN java -version
COPY ./jar/AdminPortal.jar /usr/src/AdminPortal.jar
CMD java -jar /usr/src/AdminPortal.jar