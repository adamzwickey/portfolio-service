.PHONY: clean test build-app build-images push-images

app: clean build-images push-images

clean:
	./mvnw clean

test:
	./mvnw test

build-app:
	./mvnw package

build-images: check-imagerepo
	./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=$(image-repo)/portfolio:v1

push-images: check-imagerepo
	docker push $(image-repo)/portfolio:v1

check-imagerepo:
ifndef image-repo
	$(error image-repo is undefined)
endif
