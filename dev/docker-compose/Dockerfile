FROM tomcat:8.5
COPY conf/paradise-api.conf /etc/paradise/api.conf
COPY conf/context-api.xml /usr/local/tomcat/conf/Catalina/localhost/api.xml
COPY conf/wait-for-it.sh /usr/bin/wait-for-it.sh
CMD ["catalina.sh", "run"]
