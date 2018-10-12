package com.oneops.infoblox;

import com.oneops.infoblox.model.Result;
import com.oneops.infoblox.model.a.ARec;
import com.oneops.infoblox.model.aaaa.AAAA;
import com.oneops.infoblox.model.cname.CNAME;
import com.oneops.infoblox.model.host.Host;
import com.oneops.infoblox.model.mx.MX;
import com.oneops.infoblox.model.network.Network;
import com.oneops.infoblox.model.ns.NS;
import com.oneops.infoblox.model.ptr.PTR;
import com.oneops.infoblox.model.srv.SRV;
import com.oneops.infoblox.model.ttl.TTLRec;
import com.oneops.infoblox.model.txt.TXT;
import com.oneops.infoblox.model.zone.ZoneAuth;
import com.oneops.infoblox.model.zone.ZoneDelegate;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Infoblox DNS appliance (IBA) REST interface.
 *
 * @author Suresh G
 */
public interface Infoblox {

  /**
   * By default, all the fields are not returned during a GET request. You can use the
   * <b>_return_fields</b> argument to get the desired data. You would have to explicitly mention
   * the additional fields you require with a <b>_return_fields+={requiredfield}></b>. The following
   * variables describe the fields for each record type. <b>view</b> and <b>ttl</b> fields are
   * included by default as it's part of the base {@link com.oneops.infoblox.model.Record} object.
   *
   * <p>Note: AuthZone record doesn't have any TTL field.
   */
  String A_FIELDS = "_return_fields=name,ipv4addr,view,ttl";

  String AAAA_FIELDS = "_return_fields=name,ipv6addr,view,ttl";

  String CNAME_FIELDS = "_return_fields=name,canonical,view,ttl";

  String HOST_FIELDS = "_return_fields=name,ipv4addrs,view,ttl";

  String MX_FIELDS = "_return_fields=name,mail_exchanger,preference,view,ttl";

  String NS_FIELDS = "_return_fields=name,nameserver,addresses,view,ttl";

  String PTR_FIELDS = "_return_fields=name,ipv4addr,ipv6addr,ptrdname,view,ttl";

  String SRV_FIELDS = "_return_fields=name,port,priority,target,weight,view,ttl";

  String TXT_FIELDS = "_return_fields=name,text,view,ttl";

  String ZONE_AUTH_FIELDS = "_return_fields=fqdn,view";

  String ZONE_DELEGATE_FIELDS = "_return_fields=delegate_to,fqdn,view,delegated_ttl,locked";

  String TTL_FIELDS = "_return_fields=view,ttl";

  String NETWORK_RETRIEVAL = "?_return_fields=network";
  // &_paging=1

  String NETWORK_MAXSRESULTS = "&_max_results=5000";

  /** Auth zone Record */
  @GET("zone_auth?" + ZONE_AUTH_FIELDS)
  Call<Result<List<ZoneAuth>>> queryAuthZones();

  @GET("zone_auth?" + ZONE_AUTH_FIELDS)
  Call<Result<List<ZoneAuth>>> queryAuthZone(@QueryMap(encoded = true) Map<String, String> options);

  /** Zone delegation */
  @GET("zone_delegated?" + ZONE_DELEGATE_FIELDS)
  Call<Result<List<ZoneDelegate>>> queryDelegatedZones();

  @GET("zone_delegated?" + ZONE_DELEGATE_FIELDS)
  Call<Result<List<ZoneDelegate>>> queryDelegatedZone(
      @QueryMap(encoded = true) Map<String, String> options);

  @POST("zone_delegated?" + ZONE_DELEGATE_FIELDS)
  Call<Result<ZoneDelegate>> createDelegatedZone(@Body Map<String, Object> req);

  @PUT("zone_delegated?" + ZONE_DELEGATE_FIELDS)
  Call<Result<ZoneDelegate>> modifyDelegatedZone(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, Object> req);

  /** Network */
  @GET("network" + NETWORK_RETRIEVAL + NETWORK_MAXSRESULTS)
  Call<Result<List<Network>>> queryNetwork(@QueryMap(encoded = true) Map<String, String> options);

  @GET("network" + NETWORK_RETRIEVAL)
  Call<Result<List<Network>>> queryNetwork(@Query("_max_results") Integer maxResults);

  @GET("network" + NETWORK_RETRIEVAL + NETWORK_MAXSRESULTS)
  Call<Result<List<Network>>> queryNetwork();

  /** Host Record */
  @GET("./record:host?" + HOST_FIELDS)
  Call<Result<List<Host>>> queryHostRec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:host?" + HOST_FIELDS)
  Call<Result<Host>> createHostRec(@Body Map<String, Object> req);

  /** A Record */
  @GET("./record:a?" + A_FIELDS)
  Call<Result<List<ARec>>> queryARec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:a?" + A_FIELDS)
  Call<Result<ARec>> createARec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + A_FIELDS)
  Call<Result<ARec>> modifyARec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** AAAA Record */
  @GET("./record:aaaa?" + AAAA_FIELDS)
  Call<Result<List<AAAA>>> queryAAAARec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:aaaa?" + AAAA_FIELDS)
  Call<Result<AAAA>> createAAAARec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + AAAA_FIELDS)
  Call<Result<AAAA>> modifyAAAARec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** CNAME Record */
  @GET("./record:cname?" + CNAME_FIELDS)
  Call<Result<List<CNAME>>> queryCNAMERec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:cname?" + CNAME_FIELDS)
  Call<Result<CNAME>> createCNAMERec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + CNAME_FIELDS)
  Call<Result<CNAME>> modifyCNAMERec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** MX Record */
  @GET("./record:mx?" + MX_FIELDS)
  Call<Result<List<MX>>> queryMXRec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:mx?" + MX_FIELDS)
  Call<Result<MX>> createMXRec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + MX_FIELDS)
  Call<Result<MX>> modifyMXRec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** NS Record */
  @GET("./record:ns?" + NS_FIELDS)
  Call<Result<List<NS>>> queryNSRec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:ns?" + NS_FIELDS)
  Call<Result<NS>> createNSRec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + NS_FIELDS)
  Call<Result<NS>> modifyNSRec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** PTR Record */
  @GET("./record:ptr?" + PTR_FIELDS)
  Call<Result<List<PTR>>> queryPTRRec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:ptr?" + PTR_FIELDS)
  Call<Result<PTR>> createPTRRec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + PTR_FIELDS)
  Call<Result<PTR>> modifyPTRRec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** SRV Record */
  @GET("./record:srv?" + SRV_FIELDS)
  Call<Result<List<SRV>>> querySRVRec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:srv?" + SRV_FIELDS)
  Call<Result<SRV>> createSRVRec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + SRV_FIELDS)
  Call<Result<SRV>> modifySRVRec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** TXT Record */
  @GET("./record:txt?" + TXT_FIELDS)
  Call<Result<List<TXT>>> queryTXTRec(@QueryMap(encoded = true) Map<String, String> options);

  @POST("./record:txt?" + TXT_FIELDS)
  Call<Result<TXT>> createTXTRec(@Body Map<String, Object> req);

  @PUT("./{ref}?" + TXT_FIELDS)
  Call<Result<TXT>> modifyTXTRec(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, String> req);

  /** Delete Record */
  @DELETE("./{ref}")
  Call<Result<String>> deleteRef(@Path(value = "ref", encoded = true) String ref);

  /** Modify TTL for a record */
  @PUT("./{ref}?" + TTL_FIELDS)
  Call<Result<TTLRec>> modifyTTL(
      @Path(value = "ref", encoded = true) String ref, @Body Map<String, Object> req);

  /** Logout session */
  @POST("logout")
  Call<Void> logout();
}
