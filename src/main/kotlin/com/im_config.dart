class littleFootage {
  final int entry;
  final int nom;
  final int adjust;

  littleFootage.reportBandy({
    this.entry = 10,
    this.nom = 10,
    this.adjust = 0,
  });

  factory littleFootage.fromJson(Map<String, dynamic> json) {
    return littleFootage.reportBandy(
      entry: json['messageLimit'] ?? 10,
      nom: json['callsLimit'] ?? 10,
      adjust: json['androidCost'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'messageLimit': entry,
      'callsLimit': nom,
      'androidCost': adjust,
    };
  }

  String test() {
    return "";
  }
}
