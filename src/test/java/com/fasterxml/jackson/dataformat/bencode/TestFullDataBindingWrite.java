package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.bencode.types.Torrent;
import com.fasterxml.jackson.dataformat.bencode.types.User;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("UnusedDeclaration")
public class TestFullDataBindingWrite {
    private ObjectMapper underTest;

    @Before
    public void startUp() throws IOException {
        underTest = new BEncodeMapper();
    }

    @Test
    public void testWriteValueToStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        User u = new User();
        User.Name un = new User.Name();
        un.setFirst("Joe");
        un.setLast("Sixpack");
        u.setGender(User.Gender.MALE);
        u.setUserImage(TestUtils.BINARY_DATA);
        u.setName(un);
        u.setVerified(false);

        underTest.writeValue(out, u);
        assertThat(out.toString("ISO-8859-1"), is(TestUtils.TUTORIAL_EXAMPLE_ENCODED));
    }

    @Test
    public void testWriteValueToStreamComplex() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] ubuntuIsoTorrent = TestUtils.readFileBinary("/ubuntu-13.10-desktop-amd64.iso.torrent");
        byte[] pieces = new byte[35320];

        System.arraycopy(ubuntuIsoTorrent, 0x014f, pieces, 0, pieces.length);

        Torrent t = new Torrent();
        Torrent.Info i = new Torrent.Info();

        t.setAnnounce("http://torrent.ubuntu.com:6969/announce");
        //noinspection unchecked
        t.setAnnounceList(Arrays.asList(
                Collections.singletonList("http://torrent.ubuntu.com:6969/announce"),
                Collections.singletonList("http://ipv6.torrent.ubuntu.com:6969/announce")
        ));
        t.setComment("Ubuntu CD releases.ubuntu.com");
        t.setCreationDate(1382003607);

        i.setLength(925892608L);
        i.setName("ubuntu-13.10-desktop-amd64.iso");
        i.setPieceLength(524288);
        i.setPieces(pieces);

        t.setInfo(i);

        underTest.writeValue(out, t);

        assertThat(out.toString("ISO-8859-1"), is(new String(ubuntuIsoTorrent, "ISO-8859-1")));
    }

    @Test
    public void testWriteValueToStreamComplexMultiFile() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] getStartedTorrent = TestUtils.readFileBinary("/GET-STARTED.torrent");
        byte[] pieces = new byte[35620];

        System.arraycopy(getStartedTorrent, 0x0a1e, pieces, 0, pieces.length);

        Torrent t = new Torrent();
        Torrent.Info i = new Torrent.Info();

        t.setAnnounce("udp://tracker.publicbt.com:80/announce");
        //noinspection unchecked
        t.setAnnounceList(Arrays.asList(
                Collections.singletonList("udp://tracker.publicbt.com:80/announce"),
                Collections.singletonList("udp://tracker.openbittorrent.com:80/announce"),
                Collections.singletonList("http://tracker.todium.com/780121a01736a2d19789c4e66f9e1f16611b5d58/announce")
        ));
        t.setComment("Official BitTorrent Content");
        t.setCreatedBy("BitTorrent, Inc.");
        t.setCreationDate(1371247048);
        t.setInfo(i);

        i.setFiles(Arrays.asList(
                new Torrent.Info.File(Collections.singletonList("bittorrentisnotacrime-sticker.pdf"), 51090),
                new Torrent.Info.File(Arrays.asList("How To", "How to - Remote.png"), 197070),
                new Torrent.Info.File(Arrays.asList("How To", "How to - Surf.png"), 160804),
                new Torrent.Info.File(Arrays.asList("How To", "uTorrent_Tetris.png"), 75402),
                new Torrent.Info.File(Arrays.asList("Videos", "BitTorrent Bundle.mp4"), 33251599),
                new Torrent.Info.File(Arrays.asList(
                        "Videos", "BitTorrent Sessions - Tim Ferriss- Your Book is a Startup.mp4"), 400411710),
                new Torrent.Info.File(Arrays.asList("Videos", "BitTorrent Surf.mp4"), 8562877),

                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-field-1280x1280.png"), 2336537),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-field-640x960.png"), 892167),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-projection-1280x1280.png"), 829322),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-projection-2560x1600.png"), 8723797),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-projection-640x960.png"), 475004),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-type-1280x1280.png"), 20641),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-type-2560x1600.png"), 73219),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-BT-type-640x960.png"), 14475),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-SoShare-lines-1280x1280.png"), 103049),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-SoShare-lines-2560x1600.png"), 165153),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-SoShare-lines-640x960.png"), 57747),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-SoShare-tools-1280x1280.png"), 13364),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-SoShare-tools-2560x1600.png"), 23897),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-SoShare-tools-640x960.png"), 8667),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-hummingbird-1280x1280.png"), 234093),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-hummingbird-2560x1600.png"), 406153),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-hummingbird-640x960.png"), 149274),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-swarm-1280x1280.png"), 207728),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-swarm-2560x1600.png"), 438953),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-swarm-640x960.png"), 105897),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-type-1280x1280.png"), 22923),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-type-2560x1600.png"), 90843),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "061113-UT-type-640x960.png"), 16457),
                new Torrent.Info.File(Arrays.asList("Wallpapers", "BitTorrent Wallpaper.png"), 8723797)
        ));

        i.setName("GET-STARTED");
        i.setPieceLength(262144);
        i.setPieces(pieces);
        t.setUrlList("http://apps.bittorrent.com/torrents/torrentdata/");

        underTest.writeValue(out, t);

        assertThat(out.toString("ISO-8859-1"), is(new String(getStartedTorrent, "ISO-8859-1")));
    }
}
