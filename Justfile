set windows-shell := ["powershell.exe", "-NoLogo", "-Command"]

build: format
  scala-cli --power compile --suppress-experimental-warning .

check-format:
  scalafmt . --check

format:
  scalafmt .

run:
  scala-cli --power --suppress-experimental-warning .

prepare-assets:
  rm package/assets
  mkdir package/assets
  cp assets/*.wav package/assets/
  cp assets/*.bmp package/assets/
  cp assets/*.obj package/assets/
  cp assets/*.mtl package/assets/

package: prepare-assets
  scala-cli --power package . -f -o package/skydive.jar
  scala-cli --power package . -f --native --native-mode release -o package/skydive.exe
  scala-cli --power package . -f --js --js-mode release -o package/skydive.js

clean:
  scala-cli clean .
