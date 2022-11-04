// Code generated by Wire protocol buffer compiler, do not edit.
// Source: squareup.protos.custom_options.MessageWithOptions in custom_options.proto
import Foundation
import Wire

public struct MessageWithOptions {

    public var unknownFields: Data = .init()

    public init() {
    }

}

#if !WIRE_REMOVE_EQUATABLE
extension MessageWithOptions : Equatable {
}
#endif

#if !WIRE_REMOVE_HASHABLE
extension MessageWithOptions : Hashable {
}
#endif

extension MessageWithOptions : ProtoMessage {
    public static func protoMessageTypeURL() -> String {
        return "type.googleapis.com/squareup.protos.custom_options.MessageWithOptions"
    }
}

extension MessageWithOptions : Proto2Codable {
    public init(from reader: ProtoReader) throws {
        let token = try reader.beginMessage()
        while let tag = try reader.nextTag(token: token) {
            switch tag {
            default: try reader.readUnknownField(tag: tag)
            }
        }
        self.unknownFields = try reader.endMessage(token: token)

    }

    public func encode(to writer: ProtoWriter) throws {
        try writer.writeUnknownFields(unknownFields)
    }
}

#if !WIRE_REMOVE_CODABLE
extension MessageWithOptions : Codable {
}
#endif
