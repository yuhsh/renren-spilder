java -agentlib:jdwp=transport=dt_socket,address=8001,server=y,suspend=y -jar -Xmn16m -Xms64m -Xmx128m renren.it_spilder.jar -d=config/xiaoshuo_config -spring=xiaoshuoBeans.xml 