/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.git.library;

import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Job;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration;
import org.jenkinsci.plugins.workflow.libs.LibraryResolver;
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever;

/**
 * Allows libraries to be loaded on the fly from Git.
 */
@Extension public class GitLibraryResolver extends LibraryResolver {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GitLibraryResolver.class.getName());

    @Override public boolean isTrusted() {
        return false;
    }

    @Override public Collection<LibraryConfiguration> forJob(Job<?,?> job, Map<String,String> libraryVersions) {
        List<LibraryConfiguration> libs = new ArrayList<>();
        for (Map.Entry<String,String> entry : libraryVersions.entrySet()) {
            String labUrl = entry.getKey();

            GitSCMSource gitSCMSource = null;

            String regex = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(labUrl);
            if(matcher.find()) {
                // 存在协议头的
                gitSCMSource = new GitSCMSource(null, matcher.group(), "git-lib-credentials", "*", "", true);
            }else{
                // 非git地址，默认加上https://
                gitSCMSource = new GitSCMSource(null, "https://" + labUrl + ".git", "git-lib-credentials", "*", "", true);
            }

            LibraryConfiguration lib = new LibraryConfiguration(labUrl, new SCMSourceRetriever(gitSCMSource));
            lib.setDefaultVersion("master");
            libs.add(lib);
        }
        return libs;
    }

}
