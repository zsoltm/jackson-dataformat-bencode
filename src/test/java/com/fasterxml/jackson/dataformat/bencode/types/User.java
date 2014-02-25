package com.fasterxml.jackson.dataformat.bencode.types;

@SuppressWarnings("UnusedDeclaration")
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
