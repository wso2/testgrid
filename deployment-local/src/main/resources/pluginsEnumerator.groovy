def plugins = jenkins.model.Jenkins.instance.getPluginManager().getPlugins()
println "${plugins.size()}"