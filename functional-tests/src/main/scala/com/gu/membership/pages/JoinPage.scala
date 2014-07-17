package com.gu.membership.pages

import org.openqa.selenium.{By, WebDriver}

/**
 * Created by jao on 29/05/2014.
 */
class JoinPage(driver: WebDriver) extends BaseMembershipPage(driver) {

  private def becomeAFriendLink = driver.findElement(By.xpath("//body/div[2]/section/ul/li[1]/a"))

  private def becomeAPartnerLink = driver.findElement(By.xpath("//body/div[2]/section/ul/li[2]/a"))

  private def becomeAPatronLink = driver.findElement(By.xpath("//body/div[2]/section/ul/li[3]/a"))

  def clickBecomeAFriend = {
    becomeAFriendLink.click
    new JoinFriendPage(driver)
  }

  def clickBecomeAPatron = {
    becomeAPatronLink.click
    new JoinPatronPage(driver)
  }

  def clickBecomeAPartner = {
    becomeAPartnerLink.click
    new JoinPartnerPage(driver)
  }
}