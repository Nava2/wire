// Code generated by Wire protocol buffer compiler, do not edit.
// Source: squareup.protos.kotlin.DeprecatedProto in deprecated.proto
package com.squareup.wire.protos.kotlin

import com.squareup.wire.FieldEncoding
import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import com.squareup.wire.ProtoWriter
import com.squareup.wire.ReverseProtoWriter
import com.squareup.wire.Syntax.PROTO_2
import com.squareup.wire.WireField
import com.squareup.wire.`internal`.sanitize
import kotlin.Any
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.jvm.JvmField
import okio.ByteString

public class DeprecatedProto(
  @Deprecated(message = "foo is deprecated")
  @field:WireField(
    tag = 1,
    adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  @JvmField
  public val foo: String? = null,
  unknownFields: ByteString = ByteString.EMPTY
) : Message<DeprecatedProto, DeprecatedProto.Builder>(ADAPTER, unknownFields) {
  public override fun newBuilder(): Builder {
    val builder = Builder()
    builder.foo = foo
    builder.addUnknownFields(unknownFields)
    return builder
  }

  public override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is DeprecatedProto) return false
    if (unknownFields != other.unknownFields) return false
    if (foo != other.foo) return false
    return true
  }

  public override fun hashCode(): Int {
    var result = super.hashCode
    if (result == 0) {
      result = unknownFields.hashCode()
      result = result * 37 + (foo?.hashCode() ?: 0)
      super.hashCode = result
    }
    return result
  }

  public override fun toString(): String {
    val result = mutableListOf<String>()
    if (foo != null) result += """foo=${sanitize(foo)}"""
    return result.joinToString(prefix = "DeprecatedProto{", separator = ", ", postfix = "}")
  }

  public fun copy(foo: String? = this.foo, unknownFields: ByteString = this.unknownFields):
      DeprecatedProto = DeprecatedProto(foo, unknownFields)

  public class Builder : Message.Builder<DeprecatedProto, Builder>() {
    @JvmField
    public var foo: String? = null

    @Deprecated(message = "foo is deprecated")
    public fun foo(foo: String?): Builder {
      this.foo = foo
      return this
    }

    public override fun build(): DeprecatedProto = DeprecatedProto(
      foo = foo,
      unknownFields = buildUnknownFields()
    )
  }

  public companion object {
    @JvmField
    public val ADAPTER: ProtoAdapter<DeprecatedProto> = object : ProtoAdapter<DeprecatedProto>(
      FieldEncoding.LENGTH_DELIMITED, 
      DeprecatedProto::class, 
      "type.googleapis.com/squareup.protos.kotlin.DeprecatedProto", 
      PROTO_2, 
      null
    ) {
      public override fun encodedSize(`value`: DeprecatedProto): Int {
        var size = value.unknownFields.size
        size += ProtoAdapter.STRING.encodedSizeWithTag(1, value.foo)
        return size
      }

      public override fun encode(writer: ProtoWriter, `value`: DeprecatedProto): Unit {
        ProtoAdapter.STRING.encodeWithTag(writer, 1, value.foo)
        writer.writeBytes(value.unknownFields)
      }

      public override fun encode(writer: ReverseProtoWriter, `value`: DeprecatedProto): Unit {
        writer.writeBytes(value.unknownFields)
        ProtoAdapter.STRING.encodeWithTag(writer, 1, value.foo)
      }

      public override fun decode(reader: ProtoReader): DeprecatedProto {
        var foo: String? = null
        val unknownFields = reader.forEachTag { tag ->
          when (tag) {
            1 -> foo = ProtoAdapter.STRING.decode(reader)
            else -> reader.readUnknownField(tag)
          }
        }
        return DeprecatedProto(
          foo = foo,
          unknownFields = unknownFields
        )
      }

      public override fun redact(`value`: DeprecatedProto): DeprecatedProto = value.copy(
        unknownFields = ByteString.EMPTY
      )
    }

    private const val serialVersionUID: Long = 0L
  }
}
