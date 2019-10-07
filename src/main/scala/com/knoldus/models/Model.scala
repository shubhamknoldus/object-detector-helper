package com.knoldus.models

object Model {

  case class ImageSetMessage(imageUUID: String, imagesDirUrl: String, unitId: String, firstImageId: String, imagesCount: Long)

}
