package jekytrum

import xitrum.Server

import jekytrum.model.Entries

object Boot {
  def main(args: Array[String]) {

    Entries.load
    Server.start()
  }
}
