/**
 * *** BEGIN LICENSE BLOCK ***** Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) 2012 Vipul A M <vipulnsward@gmail.com> Martin Bosslet
 * <Martin.Bosslet@googlemail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in
 * which case the provisions of the GPL or the LGPL are applicable instead of
 * those above. If you wish to allow use of your version of this file only under
 * the terms of either the GPL or the LGPL, and not to allow others to use your
 * version of this file under the terms of the CPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and other
 * provisions required by the GPL or the LGPL. If you do not delete the
 * provisions above, a recipient may use your version of this file under the
 * terms of any one of the CPL, the GPL or the LGPL.
 */
package org.jruby.ext.binyo;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import static org.jruby.RubyEnumerator.enumeratorize;
import org.jruby.RubyFixnum;
import org.jruby.RubyModule;
import org.jruby.RubyNumeric;
import org.jruby.RubyObject;
import org.jruby.RubyRange;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author Vipul A M <vipulnsward@gmail.com>
 */
public class RubyByteList extends RubyObject {

    private int size = 0;
    private byte bytes[];
    private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass type) {
            return new RubyByte(runtime, type);
        }
    };

    protected RubyByteList(Ruby runtime, RubyClass bval) {
        super(runtime, bval);
    }

    protected RubyByteList(Ruby runtime, RubyClass bval, byte[] bytes) {
        super(runtime, bval);
        this.bytes = bytes;
    }

    public static void createByteList(Ruby runtime, RubyModule binyo, RubyClass binyoError) {
        binyo.defineClassUnder("ByteListError", binyoError, binyoError.getAllocator());
        RubyClass cbytel = binyo.defineClassUnder("ByteList", null, ALLOCATOR);
        cbytel.defineAnnotatedMethods(RubyByteList.class);
        cbytel.includeModule(runtime.getEnumerable());
    }

    private IRubyObject createNewBytes(IRubyObject iString) {
        bytes = iString.asJavaString().getBytes();
        return this;
    }

    private IRubyObject createNewSize(IRubyObject iSize) {
        size = (int) RubyNumeric.num2dbl(iSize);
        bytes = new byte[size];
        return this;
    }

    @JRubyMethod
    public IRubyObject initialize(ThreadContext ctx, IRubyObject iVal) {
        return (iVal instanceof RubyString) ? createNewBytes(iVal) : createNewSize(iVal);
    }

    protected byte[] getBytes() {
        return bytes;
    }

    @JRubyMethod
    public IRubyObject each(ThreadContext ctx, Block block) {
        return block.isGiven() ? eachCommon(ctx, block) : enumeratorize(ctx.runtime, this, "each");
    }

    public IRubyObject eachCommon(ThreadContext context, Block block) {
        if (!block.isGiven()) {
            throw context.runtime.newLocalJumpErrorNoBlock();
        }
        for (int i = 0; i < size; i++) {
            block.yield(context, new RubyByte(context.getRuntime(), RubyByte.rubyByteClass, bytes[i]));
        }
        return this;
    }

    @JRubyMethod(name = {"[]", "slice"})
    public IRubyObject aRef(IRubyObject arg0, IRubyObject arg1) {
        return arefCommon(arg0, arg1);
    }

    private IRubyObject arefCommon(IRubyObject arg0, IRubyObject arg1) {
        long beg = RubyNumeric.num2long(arg0);
        if (beg < 0) {
            beg += size;
        }
        return subseq(beg, RubyNumeric.num2long(arg1));
    }

    public IRubyObject subseq(long beg, long len) {
        int realLength = size;
        if (beg > realLength || beg < 0 || len < 0) {
            return getRuntime().getNil();
        }

        if (beg + len > realLength) {
            len = realLength - beg;
            if (len < 0) {
                len = 0;
            }
        }

        if (len == 0) {
            return new RubyByteList(getRuntime(), getMetaClass(), new byte[0]);
        }

        byte[] sBytes = new byte[(int) len];
        System.arraycopy(bytes, (int) beg, sBytes, 0, (int) len);
        return new RubyByteList(getRuntime(), getMetaClass(), sBytes);
    }

    @JRubyMethod(name = "<<", required = 1)
    public RubyByteList append(IRubyObject item) {
        if (size == Integer.MAX_VALUE) {
            throw getRuntime().newArgumentError(" index too big");
        }
        return (item instanceof RubyByte) ? byteAppend((RubyByte) item) : objectAppend(item);
    }

    private RubyByteList byteAppend(RubyByte item) {
        byte[] combined = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, combined, 0, bytes.length);
        combined[bytes.length] = item.getByte();
        return this;
    }

    @JRubyMethod
    public IRubyObject to_s(ThreadContext ctx) {
        return RubyString.newString(ctx.getRuntime(), new String(bytes));
    }

    private RubyByteList objectAppend(IRubyObject item) {
        byte[] append = item.asJavaString().getBytes();
        byte[] combined = new byte[bytes.length + append.length];
        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < bytes.length ? bytes[i] : append[i - bytes.length];
        }
        bytes = combined;
        return this;
    }

    @JRubyMethod(name = "[]=")
    public IRubyObject a_set(IRubyObject arg0, IRubyObject arg1) {
        if (arg0 instanceof RubyFixnum) {
            store((int)((RubyFixnum) arg0).getLongValue(), arg1);
        } else if (arg0 instanceof RubyRange) {
            long first = RubyNumeric.num2long(((RubyRange) arg0).first());
            long last = RubyNumeric.num2long(((RubyRange) arg0).last());
            if (arg1 instanceof RubyByteList) {
                //TODO: process bytelist
                ((RubyByteList) arg1).getBytes();
            } else {
                //TODO: expand array and store bytes
                arg1.asJavaString().getBytes();
            }
        } else {
            store((int)RubyNumeric.num2long(arg0), arg1);
        }
        return arg1;
    }

    private void store(int index, IRubyObject value) {
        if (index < 0) {
            throw getRuntime().newIndexError("index " + index + " out of array");
        }
        if (value instanceof RubyByte) {
            bytes[index] = ((RubyByte) value).getByte();
        } else {
            bytes[index] = value.asJavaString().getBytes()[0];
        }

    }
}
