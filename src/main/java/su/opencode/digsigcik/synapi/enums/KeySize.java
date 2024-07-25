package su.opencode.digsigcik.synapi.enums;

import lombok.Getter;

@Getter
public enum KeySize {
    BIT_256(256),
    BIT_512(512),
    BIT_1024(1024),
    BIT_2048(2048),
    BIT_3072(3072),
    BIT_4096(4096);

    private final int size;

    KeySize(int size) {
        this.size = size;
    }

}