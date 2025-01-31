// Code generated by Wire protocol buffer compiler, do not edit.
// Source: NoPackageService in service_without_package.proto
package com.squareup.wire.protos.kotlin.services

import com.squareup.wire.GrpcCall
import com.squareup.wire.GrpcClient
import com.squareup.wire.GrpcMethod

public class GrpcNoPackageServiceClient(
  private val client: GrpcClient,
) : NoPackageServiceClient {
  override fun NoPackageMethod(): GrpcCall<NoPackageRequest, NoPackageResponse> =
      client.newCall(GrpcMethod(
      path = "/NoPackageService/NoPackageMethod",
      requestAdapter = NoPackageRequest.ADAPTER,
      responseAdapter = NoPackageResponse.ADAPTER
  ))
}
