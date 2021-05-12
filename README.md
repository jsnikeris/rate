# rate

Connects to two databases running locally and writes a report to standard out of the added, removed, and corrupted rows.

## Usage

First, start the databases:

    $ docker run -p 5432:5432 guaranteedrate/homework-pre-migration:1607545060-a7085621
    $ docker run -p 5433:5432 guaranteedrate/homework-post-migration:1607545060-a7085621

Then, generate the report:

    $ lein run > report.clj

