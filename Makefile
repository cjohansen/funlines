test:
	bin/kaocha

autotest:
	bin/kaocha --watch

funlines.jar: src/funlines/*.cljc
	clojure -M:jar

clean:
	rm -fr funlines.jar

deploy: funlines.jar
	mvn deploy:deploy-file -Dfile=funlines.jar -DrepositoryId=clojars -Durl=https://clojars.org/repo -DpomFile=pom.xml

.PHONY: test autotest deploy clean
