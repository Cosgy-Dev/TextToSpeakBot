package com.jagrosh.jdautilities.doc.standard

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandInfo(
    val name: Array<String> = [],
    val description: String = ""
)
