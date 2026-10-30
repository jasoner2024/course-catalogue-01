# Getting Started

Do these steps on the laptop you will use, before the session. They take about 30 minutes.
If a step fails, tell us which one and what it printed.

## 1. Create a GitHub account

If you do not have one, sign up at https://github.com/signup. Send us your GitHub username.

Optional: to keep your personal email address out of your commits, go to GitHub **Settings** >
**Emails** and tick **Keep my email addresses private**. Note the address that ends in
`@users.noreply.github.com`.

## 2. Install the tools

You need: Git, the GitHub CLI (`gh`), JDK 21 and Maven 3.9 or later. Skip any you already have.

**macOS** (needs Homebrew from https://brew.sh):

```bash
brew install git gh maven
brew install --cask temurin@21
```

**Windows** (in PowerShell):

```powershell
winget install --id Git.Git -e --source winget
winget install --id GitHub.cli --source winget
```

- JDK 21: download and run the Temurin 21 installer from https://adoptium.net. If the installer
  offers to set `JAVA_HOME`, accept it.
- Maven: download the binary zip from https://maven.apache.org/download.cgi, unzip it (for
  example to `C:\tools`), and add its `bin` folder to your `PATH`.
- Close and reopen your terminal. From now on, use **Git Bash** for every command.

**Linux (Ubuntu or Debian):**

```bash
sudo apt update
sudo apt install git openjdk-21-jdk maven
```

- GitHub CLI: follow https://github.com/cli/cli/blob/trunk/docs/install_linux.md
- If step 3 shows a Maven version older than 3.9, download Maven from
  https://maven.apache.org/download.cgi and put its `bin` folder first in your `PATH`.

## 3. Check the tools

```bash
git --version
gh --version
java -version
mvn -v
```

`java -version` must show 21. `mvn -v` must show 3.9 or later, and Java 21.

## 4. Sign in to GitHub from the terminal

```bash
gh auth login
```

Answer the questions: **GitHub.com**, **HTTPS**, **Yes** (use your GitHub credentials for Git),
**Login with a web browser**. Copy the one-time code it shows, press Enter, and paste the code in
the browser page that opens.

Then let Git use this sign-in and check it worked:

```bash
gh auth setup-git
gh auth status
```

`gh auth status` must say you are logged in.

## 5. Tell Git your name and email

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Use your `@users.noreply.github.com` address here if you chose to keep your email private.

## 6. Accept the invitation

Open the invitation link we sent you, or go to
`https://github.com/<owner>/<repository>/invitations`, and click **Accept invitation**.

## 7. Clone the repository and build it once

This downloads the build dependencies now, so you do not wait for them during the session.

```bash
gh repo clone <owner>/<repository>
cd <repository>
mvn -B test
```

The last lines must say `BUILD SUCCESS`.

## 8. Check that you can push

```bash
git switch -c access-check
git push -u origin access-check
git push origin --delete access-check
git switch main
```

Both push commands must succeed.

## 9. Tell us it worked

Reply to our email to say steps 3, 7 and 8 worked, or which step failed.

On the day: join the video call from this laptop, be ready to share your screen, and run
`git pull` in the repository folder. Please do not change the code before the session.
