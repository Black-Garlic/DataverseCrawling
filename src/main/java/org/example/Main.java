package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static final String MONTH = "Feb";
    private static final String MONTH_NUM = "02";
    private static final List<String> commitAddressList = new ArrayList<>();
    private static final List<Set<Commit>> dataList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        setCommitAddress();
        generateDataList();

        for (String commitAddress : commitAddressList) {
            crawlingCommitMessage(commitAddress);
        }

        exportCSV();

        System.out.println("done");
    }

    public static void setCommitAddress() {
        commitAddressList.add("https://github.com/login?return_to=https%3A%2F%2Fgithub.com%2Fsteve-kyungjin%2Fkakaocms%2Fcommits%2Fmain");
        commitAddressList.add("https://github.com/login?return_to=https%3A%2F%2Fgithub.com%2Fsteve-kyungjin%2Fkakaocms%2Fcommits%2Fdev");
    }

    public static void generateDataList() {
        for (int i = 0; i < 31; i++) {
            Set<Commit> commitList = new HashSet<>();
            dataList.add(commitList);
        }
    }

    public static void crawlingCommitMessage(String commitAddress) throws InterruptedException {
        WebDriver driver = new ChromeDriver();

        driver.get(commitAddress);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }

        WebElement loginId = driver.findElement(By.id("login_field"));
        loginId.sendKeys("깃헙 계정");

        WebElement loginPassword = driver.findElement(By.id("password"));
        loginPassword.sendKeys("깃헙 비밀번호");

        List<WebElement> loginButton = driver.findElements(By.name("commit"));
        loginButton.get(0).click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }

        while (true) {
            List<WebElement> timeLineItem = driver.findElements(By.className("TimelineItem"));

            for (WebElement webElement : timeLineItem) {
                WebElement timeLineItemBody = webElement.findElement(By.className("TimelineItem-body"));

                String commitDateText = timeLineItemBody.findElement(By.className("text-normal")).getText();

                if (!commitDateText.contains(MONTH)) {
                    driver.quit();
                    return;
                } else {
                    String[] splitMonth = commitDateText.split(MONTH + " ");
                    String[] splitComma = splitMonth[1].split(",");
                    int dateIndex = Integer.parseInt(splitComma[0]) - 1;

                    List<WebElement> commitMessageBoxRow = timeLineItemBody.findElement(By.className("Box")).findElements(By.className("Box-row"));

                    for (int i = commitMessageBoxRow.size() - 1; i >= 0; i--) {
                        String commitMessage = commitMessageBoxRow.get(i).findElement(By.className("Details")).findElement(By.className("mb-1")).findElement(By.className("markdown-title")).getText();

                        if (!commitMessage.contains("Merge") && !commitMessage.contains("Revert")) {
                            Commit commit = new Commit();
                            String commitMessageWithoutType = "";
                            String type = "";

                            if (commitMessage.contains("[CLIENT] ")) {
                                commitMessageWithoutType = commitMessage.substring(9);
                                type = "FE";
                            } else if (commitMessage.contains("[SERVER] ")) {
                                commitMessageWithoutType = commitMessage.substring(9);
                                type = "WAS";
                            } else if (commitMessage.contains("[etl] ")) {
                                commitMessageWithoutType = commitMessage.substring(6);
                                type = "DATA";
                            } else if (commitMessage.contains("[UI] ")) {
                                commitMessageWithoutType = commitMessage.substring(5);
                                type = "UI";
                            } else {
                                commitMessageWithoutType = commitMessage;
                                type = "FE / WAS";
                            }

                            commit.setType(type);
                            commit.setCommitMessages(commitMessageWithoutType);

                            dataList.get(dateIndex).add(commit);
                        }
                    }
                }
            }

            WebElement olderButton = driver.findElement(By.className("paginate-container")).findElement(By.className("BtnGroup")).findElements(By.className("BtnGroup-item")).get(1);
            olderButton.click();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new InterruptedException();
            }
        }
    }

    public static void exportCSV() throws IOException {
        String createFile = "./commit.csv";
        FileOutputStream fos = new FileOutputStream(createFile, false);
        OutputStreamWriter out = new OutputStreamWriter(fos, "MS949");
        int index = 1;
        int date = 1;

        for (Set<Commit> commitSet : dataList) {
            if (commitSet.size() != 0) {
                for (Commit commit : commitSet) {
                    out.write(String.valueOf(index++));
                    out.write(",");
                    out.write(commit.getType());
                    out.write(",");
                    out.write("\"");
                    out.write(commit.getCommitMessages());
                    out.write("\",");

                    if (date < 10) {
                        out.write(MONTH_NUM + "월" + "0");
                        out.write(String.valueOf(date));
                        out.write("일");
                        out.write(",");
                    } else {
                        out.write(MONTH_NUM + "월");
                        out.write(String.valueOf(date));
                        out.write("일");
                        out.write(",");
                    }

                    out.write("정상\n");
                }
            }

            date++;
        }

        out.close();
    }
}