# Overview

This project contains [Jackson](http://http://wiki.fasterxml.com/JacksonHome) extension component for reading and
writing [Bencode](http://en.wikipedia.org/wiki/Bencode) encoded data, either as "raw" data (sequence of
String arrays), or via data binding to/from Java Objects (POJOs).

The primary objective of this project is to take advantage of Jackson's easy to use and fast object mapper, and make
serializing and de-serializing Bencoded content pussible using either full data-binding or streaming (tree model is not
supported yet).

Project is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

# Usage

As it is a data type extension to Jackson, it could be used like the usual JSON data format.

Given the following Bencoded data - identical counterpart of JSON example, used as a data binding example in [Jackson in
5 minutes tutorial](http://wiki.fasterxml.com/JacksonInFiveMinutes) (please note that actual binary data replaced with
asterisks, as Bencode format has raw binary representation):

```
d6:gender4:MALE4:named5:first3:Joe4:last7:Sixpacke9:userImage5:*****8:verified5:falsee
```

It could be turned into a Java POJO with the following code:

```java
ObjectMapper mapper = new BEncodeMapper();
User u = mapper.readValue(new File("user.bencode"), User.class)
```

Or similarly a Java POJO could be turned into a Bencoded stream:

```java
mapper.writeValue(new File("user.bencode"), u);
```

Where the user class used above is a simple POJO:

```
public class User {
    public enum Gender { MALE, FEMALE }

    public static class Name {
        private String first, last;

        public String getFirst() { return first; }
        public String getLast() { return last; }

        public void setFirst(String s) { first = s; }
        public void setLast(String s) { last = s; }
    }

    private Gender gender;
    private Name name;
    private boolean isVerified;
    private byte[] userImage;

    public Name getName() { return name; }
    public boolean isVerified() { return isVerified; }
    public Gender getGender() { return gender; }
    public byte[] getUserImage() { return userImage; }

    public void setName(Name n) { name = n; }
    public void setVerified(boolean b) { isVerified = b; }
    public void setGender(Gender g) { gender = g; }
    public void setUserImage(byte[] b) { userImage = b; }
}
```

Of course the POJOs could be decorated with the usual Jackson annotations like `@JsonProperty`. There is a more complex
class within tests, `com.fasterxml.jackson.dataformat.bencode.types.Torrent`, which represents a complete
[BitTorent](http://en.wikipedia.org/wiki/Bittorent) file.

# Status

Initial release with decent unit test coverage. Ready to use, but might develop some unexpected surprises.

Next steps:

 * Add benchmarks, optimize to reach a level where this is as fast as JSON - theoretically it should be, cause Bencode
    is simpler than JSON.
 * Add proper encoding support; currently UTF-8 is hardwired as default.
 * Add some useful features.
