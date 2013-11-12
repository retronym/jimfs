/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jimfs.internal;

import static com.google.jimfs.internal.Util.nextPowerOf2;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * {@link MemoryDisk} using byte arrays for blocks.
 *
 * @author Colin Decker
 */
final class HeapMemoryDisk extends MemoryDisk {

  private byte[][] blocks = new byte[4096][];

  HeapMemoryDisk() {
    this(DEFAULT_BLOCK_SIZE);
  }

  /**
   * Creates a disk with the given block size.
   */
  public HeapMemoryDisk(int blockSize) {
    super(blockSize);
  }

  @Override
  protected int allocateMoreBlocks(int count) {
    int newBlockCount = blockCount() + count;
    if (newBlockCount > blocks.length) {
      // using nextPowerOf2 instead of multiplying blocks.length * 2 until it's >= newBlockCount
      blocks = Arrays.copyOf(blocks, nextPowerOf2(newBlockCount));
    }

    for (int i = blockCount(); i < newBlockCount; i++) {
      blocks[i] = new byte[blockSize];
      free.add(i);
    }

    return count;
  }

  @Override
  public int zero(int block, int offset, int len) {
    Util.zero(blocks[block], offset, len);
    return len;
  }

  @Override
  public void copy(int block, int copy) {
    System.arraycopy(blocks[block], 0, blocks[copy], 0, blockSize);
  }

  @Override
  public void put(int block, int offset, byte b) {
    blocks[block][offset] = b;
  }

  @Override
  public int put(int block, int offset, byte[] b, int off, int len) {
    System.arraycopy(b, off, blocks[block], offset, len);
    return len;
  }

  @Override
  public int put(int block, int offset, ByteBuffer buf) {
    int len = Math.min(blockSize - offset, buf.remaining());
    buf.get(blocks[block], offset, len);
    return len;
  }

  @Override
  public byte get(int block, int offset) {
    return blocks[block][offset];
  }

  @Override
  public int get(int block, int offset, byte[] b, int off, int len) {
    System.arraycopy(blocks[block], offset, b, off, len);
    return len;
  }

  @Override
  public int get(int block, int offset, ByteBuffer buf, int len) {
    buf.put(blocks[block], offset, len);
    return len;
  }

  @Override
  public ByteBuffer asByteBuffer(int block, int offset, int len) {
    return ByteBuffer.wrap(blocks[block], offset, len);
  }
}
