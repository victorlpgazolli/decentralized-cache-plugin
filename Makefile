.PHONY: run_sample publish_local clear_cache

run_sample: publish_local
	cd sample && ../gradlew clean test --build-cache --info

publish_local:
	./gradlew publishToMavenLocal publishPluginMavenPublicationToMavenLocal

clear_cache:
	rm -rf sample/build
	for i in 1 2; do \
		echo "container=ipfs-pc-$$i"; \
		docker exec "ipfs-pc-$$i" sh -c 'ipfs pin ls --type recursive | cut -d" " -f1 | xargs -I {} ipfs pin rm {}'; \
		docker exec "ipfs-pc-$$i" ipfs repo gc; \
	done