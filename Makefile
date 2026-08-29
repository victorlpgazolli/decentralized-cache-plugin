.PHONY: run_sample publish_local clear_cache docker_clear_cache docker_start

run_sample: publish_local
	cd sample && ../gradlew clean test --build-cache --info

publish_local:
	./gradlew publishToMavenLocal publishPluginMavenPublicationToMavenLocal

docker_clear_cache:
	rm -rf sample/build
	for i in 1 2; do \
		echo "container=ipfs-pc-$$i"; \
		docker exec "ipfs-pc-$$i" sh -c 'ipfs pin ls --type recursive | cut -d" " -f1 | xargs -I {} ipfs pin rm {}'; \
		docker exec "ipfs-pc-$$i" ipfs repo gc; \
	done

clear_cache:
	rm -rf sample/build
	ipfs pin ls --type recursive | cut -d" " -f1 | xargs -I {} ipfs pin rm {}
	ipfs files rm -r /local-ipfs-gradle-cache
	ipfs repo gc

docker_start:
	docker compose up -d
	sleep 10
	./connect-peers.sh


#docker_run:
#	docker exec -it ipfs-pc-1 bash