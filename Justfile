set windows-shell := ["powershell.exe", "-NoLogo", "-Command"]

package-path := "package"
resources-path := package-path / "resources"
releases-path := package-path / "releases"

java-release-path := releases-path / "skydive-java"
native-release-path := releases-path / "skydive-native"
js-release-path := releases-path / "skydive-js"

build: format
  scala-cli --power compile --suppress-experimental-warning .

check-format:
  scalafmt . --check

format:
  scalafmt .

run:
  scala-cli --power --suppress-experimental-warning .

prepare-assets:
  rm -rf {{resources-path}}
  mkdir -p {{resources-path}}/assets
  cp assets/*.wav {{resources-path}}/assets/
  cp assets/*.bmp {{resources-path}}/assets/
  cp assets/*.obj {{resources-path}}/assets/
  cp assets/*.mtl {{resources-path}}/assets/

package-java: prepare-assets
  rm -rf {{java-release-path}}
  mkdir -p {{java-release-path}}

  scala-cli --power package . -f --embed-resources --resource-dirs {{resources-path}} -o {{java-release-path}}/skydive.jar

package-native: prepare-assets
  rm -rf {{native-release-path}}
  mkdir -p {{native-release-path}}

  scala-cli --power package . -f --embed-resources --resource-dirs {{resources-path}} --native --native-mode release -o {{native-release-path}}/skydive

package-js: prepare-assets
  rm -rf {{js-release-path}}
  mkdir -p {{js-release-path}}

  cp -r {{resources-path}}/assets {{js-release-path}}/assets
  cp package/index.html {{js-release-path}}/index.html
  scala-cli --power package . -f --js --js-mode release -o {{js-release-path}}/skydive.js

package: package-java package-native package-js

clean:
  scala-cli clean .
