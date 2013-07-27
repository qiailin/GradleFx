/*
 * Copyright (c) 2011 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradlefx.tasks.mobile

import org.apache.commons.lang.StringUtils
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction
import org.gradlefx.cli.CompilerOption
import org.gradlefx.conventions.AIRMobileConvention
import org.gradlefx.conventions.FlexType
import org.gradlefx.tasks.AdtTask

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Chaos Encoder</a>
 */
class BaseAirMobilePackage extends AdtTask {

    public BaseAirMobilePackage() {
        super()
        description = "Packages the generated swf file into an mobile package";
        adtWorkDir = flexConvention.air.packageWorkDir
    }

    AIRMobileConvention getAirMobile() {
        flexConvention.airMobile
    }

    String getTarget() {
        airMobile.target
    }

    def getOutputPath() {
        return InstallAppUtils.getReleaseOutputPath(flexConvention, project)
    }

    @TaskAction
    def launch() {
        addArg CompilerOption.PACKAGE.optionName
        addArg CompilerOption.TARGET.optionName
        addArg target

        if (StringUtils.isNotEmpty(flexConvention.airMobile.provisioning_profile)) {
            addArgs CompilerOption.PROVISIONING_PROFILE.optionName, flexConvention.airMobile.provisioning_profile
        }

        addArgs CompilerOption.STORE_TYPE.optionName,
                "pkcs12",
                CompilerOption.KEYSTORE.optionName,
                flexConvention.air.keystore,
                CompilerOption.STOREPASS.optionName,
                flexConvention.air.storepass

        addArgs outputPath
        addArgs project.file(flexConvention.air.applicationDescriptor)

        addArgs CompilerOption.CHANGE_DIRECTORY.optionName, "${project.buildDir.absolutePath}", "${flexConvention.output}.${FlexType.swf}"

        flexConvention.air.includeFileTrees.each { ConfigurableFileTree fileTree ->
            addArgs CompilerOption.CHANGE_DIRECTORY.optionName
            addArgs fileTree.dir.absolutePath

            fileTree.visit { FileTreeElement file ->
                if (!file.isDirectory()) {
                    addArgs file.relativePath
                }
            }
        }

        if (StringUtils.isNotEmpty(flexConvention.airMobile.extensionDir)) {
            addArg(CompilerOption.EXTDIR.optionName)
            addArg(flexConvention.airMobile.extensionDir)
        }

        addPlatformSdkParams()

        super.launch()
    }

    def addPlatformSdkParams() {
        addArgs CompilerOption.PLATFORM_SDK.optionName, flexConvention.airMobile.platformSdk
    }
}
