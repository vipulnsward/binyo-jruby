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
 * Copyright (C) 2012
 * Vipul A M <vipulnsward@gmail.com> 
 * Martin Bosslet <Martin.Bosslet@googlemail.com>
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
import org.jruby.RubyInteger;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author Vipul A M <vipulnsward@gmail.com>
 */
public class RubyByte extends RubyObject {

    protected static RubyClass rubyByteClass=null;
    private byte byteval;
    private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass type) {
            return new RubyByte(runtime, type);
        }
    };

    public void setByte(byte b) {
        this.byteval = b;
    }
    
    public byte getByte(){
        return byteval; 
    }

    protected RubyByte(Ruby runtime, RubyClass bytec) {
        super(runtime, bytec);
    }
    
    protected RubyByte(Ruby runtime, RubyClass bytec, byte b) {
        super(runtime, bytec);
        byteval=b;
    }

    public static void createByte(Ruby runtime, RubyModule binyo, RubyClass binyoError) {
        RubyClass byteError = binyo.defineClassUnder("ByteError", binyoError, binyoError.getAllocator());
        rubyByteClass = binyo.defineClassUnder("Byte", null, ALLOCATOR);
        rubyByteClass.defineAnnotatedMethods(RubyByte.class);
    }

    @JRubyMethod
    public IRubyObject initialize(ThreadContext ctx, IRubyObject ibyteval) {
        byteval = (byte) (ibyteval.asString().getBytes()[0]);
        return this;
    }

    @JRubyMethod(name = {">>"})
    public IRubyObject rShift(ThreadContext ctx, IRubyObject vshift) {
       return new RubyByte(ctx.getRuntime(), getMetaClass(), (byte)(byteval >> RubyInteger.num2int(vshift)));
    }

    @JRubyMethod(name = {"&"})
    public IRubyObject andOp(ThreadContext ctx, IRubyObject andBy) {
        return new RubyByte(ctx.getRuntime(), getMetaClass(), (byte) (byteval & RubyInteger.num2int(andBy)));
    }

    @JRubyMethod
    public IRubyObject to_i(ThreadContext ctx) {
        return RubyInteger.dbl2num(ctx.getRuntime(), (double) byteval);

    }
}
