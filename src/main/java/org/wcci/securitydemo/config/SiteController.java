package org.wcci.securitydemo.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wcci.securitydemo.post.ImageUploadService;
import org.wcci.securitydemo.post.Post;
import org.wcci.securitydemo.post.PostRepository;


@Controller
public class SiteController {

    @Resource
    private PostRepository postRepo;

    @Resource
    ImageUploadService uploader;

    @RequestMapping("/")
    public String home() {
        return "auth/login";
    }

    @RequestMapping("/login")
    public String login() {
        return "auth/login";
    }

    @RequestMapping("/posts")
    public String seePosts(Model model) {
        model.addAttribute("postsModel", postRepo.findAll());
        return "posts";
    }


    @RequestMapping("/admin/testing")
    @ResponseBody
    public String secure() {
        return "admin-only.html";
    }


    @RequestMapping("/admin/posts") // only accessible from admin side (try hitting on this endpoint as a
    // guest and access will be forbidden)
    public String addPost(Model model) {
        model.addAttribute("postsModel", postRepo.findAll());
        return "post";
    }

    @PostMapping("/admin/addPost")
    public String addPostWithImage(@RequestParam(value = "postContent", required = true) String content,
                                   @RequestParam("imageFile") MultipartFile imageFile) throws Exception {

        String virtualFileUrl = uploader.uploadMultipartFile(imageFile);
        System.out.println(content);
        postRepo.save(new Post(content, "/uploads/" + virtualFileUrl));

        return "redirect:/posts";
    }

    @GetMapping("/uploads/{file:.+}") // allows "." to be part of PathVariable instead of truncating it
    public void serveImage(HttpServletRequest request, HttpServletResponse response,
                           @PathVariable("file") String fileName) throws Exception {

        // Resolve path of requested file
        File requestedFile = uploader.getUploadedFile(fileName);

        // Ensure requested item exists and is a file
        if (!requestedFile.exists() || !requestedFile.isFile()) {
            throw new Exception();
        }

        // Determine and set correct content type of response
        String fileContentType = Files.probeContentType(requestedFile.toPath());
        response.setContentType(fileContentType);

        // Serve file by streaming it directly to the response
        InputStream in = new FileInputStream(requestedFile);
        IOUtils.copy(in, response.getOutputStream());
    }

}