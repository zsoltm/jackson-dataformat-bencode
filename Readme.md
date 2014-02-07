
A good standalone impl:
    https://github.com/ffbit/bencode
    
https://github.com/FasterXML/jackson-dataformat-csv/

Todo:
    - performance check; consider custom buffering, byte array API methods
    - measure performance improvement
            - when Write context's child contexts are cached
            - of integer toString avoidance.
