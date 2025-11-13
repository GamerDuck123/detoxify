package me.gamerduck.detoxify.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class DetoxifyPluginLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("ai.djl.huggingface:tokenizers:0.25.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.microsoft.onnxruntime:onnxruntime:1.19.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.spongepowered:configurate-hocon:4.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.spongepowered:configurate-core:4.2.0"), null));
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        classpathBuilder.addLibrary(resolver);
    }
}
