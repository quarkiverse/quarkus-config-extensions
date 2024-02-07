package io.quarkiverse.quarkus.git.config.test.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitRepository {

    public static GitRepository of(Path path) {
        return new GitRepository(path);
    }

    public static void free(Path path) {
        if (path.toFile().exists()) {
            try {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
            }
        }
    }

    private Git git;
    private Path path;
    private boolean initialized;

    public GitRepository init() {
        if (!initialized) {
            try {
                git = Git.init().setInitialBranch("main").setDirectory(path.toFile()).call();
                initialized = true;
            } catch (IllegalStateException | GitAPIException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Already initialized");
        }
        return this;
    }

    public GitRepository addFile(File file, String toFileName) {
        if (initialized) {
            try (var fos = new FileOutputStream(path.resolve(toFileName).toFile())) {
                Files.copy(file.toPath(), fos);
                git.add().addFilepattern(".").call();
                git.commit().setMessage(toFileName).call();
            } catch (IOException | GitAPIException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Not yet initialized");
        }
        return this;
    }

    public GitRepository checkout(String ref) {
        if (initialized) {
            try {
                git.checkout().setName(ref).call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Not yet initialized");
        }
        return this;
    }

    public GitRepository branch(String branchName) {
        if (initialized) {
            try {
                git.branchCreate().setName(branchName).call();
                git.checkout().setName(branchName).call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Not yet initialized");
        }
        return this;
    }

    public GitRepository tag(String tagName) {
        if (initialized) {
            try {
                git.tag().setName(tagName).call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Not yet initialized");
        }
        return this;
    }

    public Path getPath() {
        return path;
    }

    private GitRepository(Path path) {
        this.path = path;
    }
}
