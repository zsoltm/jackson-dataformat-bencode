
A good standalone impl:
    https://github.com/ffbit/bencode

Reference custom dataformat impl:
    https://github.com/FasterXML/jackson-dataformat-csv/

Todo:
    - update this readme
    - encoding support; default could stay UTF-8
    - performance check; consider custom buffering, byte array API methods
    - measure performance improvement
            - when Write context's child contexts are cached
            - of integer toString avoidance.
