# YCombinator News client
This is a WIP and study into embedded JPA.

Module system doesn't work, due to some dependencies' lack of module-info, so no jlink goodness.

* Bottom slider sets how many stories to show at a time.
* Drag mouse left or right on an Article to set a story as read.
* Drag mouse up or down on an Article to open Comment window for.
* Comments are fetched on demand, but persisted for fast access later.
* Resize window by dragging bottom-right corner.

DB file is saved in either of:
* `System.getenv("APPDATA")`
* `System.getenv("TEMP")`
* `System.getenv("TMP")` 

If none of those are available, a Save Dialogue will show asking you to select a directory.

---

## Prerequisites:
* JDK >= 10
* Maven >= 3.5.4

## Running:
* `mvn clean package`
* `.vscode/build.sh execute`

Or:

* `mvn clean package`
* `java -jar main/target/ycombinator-news-client-1.0-SNAPSHOT.jar`

---

https://www.webarity.com