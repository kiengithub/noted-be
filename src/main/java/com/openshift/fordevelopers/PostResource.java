package com.openshift.fordevelopers;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

  public PostResource() {
  }

  @GET
  public Response list() {
    // Đọc thật từ MongoDB (Post.listAll() — static method Panache tự sinh cho PanacheMongoEntity),
    // KHÔNG đọc từ biến RAM nữa.
    List<Post> posts = Post.listAll();
    return Response.ok(posts).build();
  }

  @POST
  public Response add(Post post) {
    // Ghi thật xuống MongoDB (post.persist() — PanacheMongoEntityBase.persist(), verify trực tiếp
    // qua source Quarkus 1.4.2.Final: extensions/panache/mongodb-panache/.../PanacheMongoEntityBase.java).
    // Bản cũ chỉ có lastPosts.add(post) — không đụng gì tới Mongo, mất hết khi pod restart.
    post.persist();
    return Response.ok().build();
  }

  @DELETE
  public Response delete(Post post) {
    // Không dùng post.id (frontend gửi lên object không có id thật của Mongo cho dữ liệu cũ) —
    // so khớp bằng equals() (title/content/timestamp, Post.java đã override đúng), tìm trong dữ
    // liệu Mongo thật rồi gọi p.delete() (PanacheMongoEntityBase.delete()) để xoá đúng document đó.
    List<Post> posts = Post.listAll();
    for (Post p : posts) {
      if (p.equals(post)) {
        p.delete();
        return Response.ok().build();
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
