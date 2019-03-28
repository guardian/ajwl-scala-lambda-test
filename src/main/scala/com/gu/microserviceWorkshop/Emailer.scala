package com.gu.microserviceWorkshop

import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sns.model.{CreateTopicRequest, SubscribeRequest}
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClientBuilder}
import com.amazonaws.services.sns.model.CreateTopicRequest
import com.amazonaws.services.sns.model.PublishRequest

import scala.io.Source
import scala.util.Try

object EmailerTest extends App {

  override def main (args: Array[String]): Unit = {
    Emailer(args(0))
  }
}


object Emailer {
  def apply(email: String) = {
    // create a topic
    val createTopicRequest = new CreateTopicRequest("MyFirstTopic")
    val createTopicResponse = AwsSNS.client.createTopic(createTopicRequest)
    val topicArn: String = createTopicResponse.getTopicArn
    println("TopicArn:" + createTopicResponse.getTopicArn)

    //subscribe to topic
    val subscribeRequest: SubscribeRequest = new SubscribeRequest(topicArn, "email", email)
    AwsSNS.client.subscribe(subscribeRequest)
    println("SubscribeRequest: " + AwsSNS.client.getCachedResponseMetadata(subscribeRequest))
    println("To confirm the subscription, check your email.")


    val msg = "Inspiration can often spring from your email! Read this now!"
    val publishRequest = new PublishRequest(topicArn, msg)
    val publishResponse = AwsSNS.client.publish(publishRequest)
    println("MessageId: " + publishResponse.getMessageId)
    }
}

object Aws {
  val ProfileName = "developerPlayground"
  lazy val CredentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider(ProfileName),
    new InstanceProfileCredentialsProvider(false)
  )
}


object AwsSNS {
  val client: AmazonSNS =
    AmazonSNSClientBuilder
      .standard()
      .withRegion(Regions.EU_WEST_1)
      .withCredentials(Aws.CredentialsProvider)
      .build()
}


object AwsS3 {
  implicit val client: AmazonS3 =
    AmazonS3ClientBuilder
      .standard()
      .withRegion(Regions.EU_WEST_1)
      .withCredentials(Aws.CredentialsProvider)
      .build()
}


object AwsS3Get {

  def apply(bucket: String, key: String): Try[String] = {
    val request = new GetObjectRequest(bucket, key)
    for {
      s3Stream <- Try(AwsS3.client.getObject(request).getObjectContent)
      contentString <- Try(Source.fromInputStream(s3Stream).mkString)
      _ <- Try(s3Stream.close())
    } yield contentString
  }

}

