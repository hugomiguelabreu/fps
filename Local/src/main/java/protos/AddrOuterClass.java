// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: addr

package protos;

public final class AddrOuterClass {
  private AddrOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface AddrOrBuilder extends
      // @@protoc_insertion_point(interface_extends:protos.Addr)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string addr = 1;</code>
     */
    String getAddr();
    /**
     * <code>string addr = 1;</code>
     */
    com.google.protobuf.ByteString
        getAddrBytes();

    /**
     * <code>int32 portNumber = 2;</code>
     */
    int getPortNumber();
  }
  /**
   * Protobuf type {@code protos.Addr}
   */
  public  static final class Addr extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:protos.Addr)
      AddrOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use Addr.newBuilder() to construct.
    private Addr(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private Addr() {
      addr_ = "";
      portNumber_ = 0;
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Addr(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              String s = input.readStringRequireUtf8();

              addr_ = s;
              break;
            }
            case 16: {

              portNumber_ = input.readInt32();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return protos.AddrOuterClass.internal_static_protos_Addr_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return protos.AddrOuterClass.internal_static_protos_Addr_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              protos.AddrOuterClass.Addr.class, protos.AddrOuterClass.Addr.Builder.class);
    }

    public static final int ADDR_FIELD_NUMBER = 1;
    private volatile Object addr_;
    /**
     * <code>string addr = 1;</code>
     */
    public String getAddr() {
      Object ref = addr_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        addr_ = s;
        return s;
      }
    }
    /**
     * <code>string addr = 1;</code>
     */
    public com.google.protobuf.ByteString
        getAddrBytes() {
      Object ref = addr_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        addr_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int PORTNUMBER_FIELD_NUMBER = 2;
    private int portNumber_;
    /**
     * <code>int32 portNumber = 2;</code>
     */
    public int getPortNumber() {
      return portNumber_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (!getAddrBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, addr_);
      }
      if (portNumber_ != 0) {
        output.writeInt32(2, portNumber_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!getAddrBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, addr_);
      }
      if (portNumber_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, portNumber_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof protos.AddrOuterClass.Addr)) {
        return super.equals(obj);
      }
      protos.AddrOuterClass.Addr other = (protos.AddrOuterClass.Addr) obj;

      boolean result = true;
      result = result && getAddr()
          .equals(other.getAddr());
      result = result && (getPortNumber()
          == other.getPortNumber());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + ADDR_FIELD_NUMBER;
      hash = (53 * hash) + getAddr().hashCode();
      hash = (37 * hash) + PORTNUMBER_FIELD_NUMBER;
      hash = (53 * hash) + getPortNumber();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static protos.AddrOuterClass.Addr parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static protos.AddrOuterClass.Addr parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static protos.AddrOuterClass.Addr parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static protos.AddrOuterClass.Addr parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static protos.AddrOuterClass.Addr parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static protos.AddrOuterClass.Addr parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(protos.AddrOuterClass.Addr prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code protos.Addr}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:protos.Addr)
        protos.AddrOuterClass.AddrOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return protos.AddrOuterClass.internal_static_protos_Addr_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return protos.AddrOuterClass.internal_static_protos_Addr_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                protos.AddrOuterClass.Addr.class, protos.AddrOuterClass.Addr.Builder.class);
      }

      // Construct using protos.AddrOuterClass.Addr.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        addr_ = "";

        portNumber_ = 0;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return protos.AddrOuterClass.internal_static_protos_Addr_descriptor;
      }

      public protos.AddrOuterClass.Addr getDefaultInstanceForType() {
        return protos.AddrOuterClass.Addr.getDefaultInstance();
      }

      public protos.AddrOuterClass.Addr build() {
        protos.AddrOuterClass.Addr result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public protos.AddrOuterClass.Addr buildPartial() {
        protos.AddrOuterClass.Addr result = new protos.AddrOuterClass.Addr(this);
        result.addr_ = addr_;
        result.portNumber_ = portNumber_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof protos.AddrOuterClass.Addr) {
          return mergeFrom((protos.AddrOuterClass.Addr)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(protos.AddrOuterClass.Addr other) {
        if (other == protos.AddrOuterClass.Addr.getDefaultInstance()) return this;
        if (!other.getAddr().isEmpty()) {
          addr_ = other.addr_;
          onChanged();
        }
        if (other.getPortNumber() != 0) {
          setPortNumber(other.getPortNumber());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        protos.AddrOuterClass.Addr parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (protos.AddrOuterClass.Addr) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private Object addr_ = "";
      /**
       * <code>string addr = 1;</code>
       */
      public String getAddr() {
        Object ref = addr_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          addr_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string addr = 1;</code>
       */
      public com.google.protobuf.ByteString
          getAddrBytes() {
        Object ref = addr_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          addr_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string addr = 1;</code>
       */
      public Builder setAddr(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }

        addr_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string addr = 1;</code>
       */
      public Builder clearAddr() {

        addr_ = getDefaultInstance().getAddr();
        onChanged();
        return this;
      }
      /**
       * <code>string addr = 1;</code>
       */
      public Builder setAddrBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

        addr_ = value;
        onChanged();
        return this;
      }

      private int portNumber_ ;
      /**
       * <code>int32 portNumber = 2;</code>
       */
      public int getPortNumber() {
        return portNumber_;
      }
      /**
       * <code>int32 portNumber = 2;</code>
       */
      public Builder setPortNumber(int value) {

        portNumber_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 portNumber = 2;</code>
       */
      public Builder clearPortNumber() {

        portNumber_ = 0;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:protos.Addr)
    }

    // @@protoc_insertion_point(class_scope:protos.Addr)
    private static final protos.AddrOuterClass.Addr DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new protos.AddrOuterClass.Addr();
    }

    public static protos.AddrOuterClass.Addr getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Addr>
        PARSER = new com.google.protobuf.AbstractParser<Addr>() {
      public Addr parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Addr(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Addr> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<Addr> getParserForType() {
      return PARSER;
    }

    public protos.AddrOuterClass.Addr getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_protos_Addr_descriptor;
  private static final
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_protos_Addr_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\004addr\022\006protos\"(\n\004Addr\022\014\n\004addr\030\001 \001(\t\022\022\n\n" +
      "portNumber\030\002 \001(\005b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_protos_Addr_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_protos_Addr_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_protos_Addr_descriptor,
        new String[] { "Addr", "PortNumber", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
