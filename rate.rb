opponents = ['pusher', 'expando', 'expando2']
maps = ['lanes', 'onetower', 'sepulchre', 'shapes', 'twos', 'trenches']

def play(map, us, opponent)
    custom = IO.read('bc.conf').
        sub(/bc.game.maps=.*$/, "bc.game.maps=#{map}").
        sub(/bc.game.team-a.*$/, "bc.game.team-a=#{us}").
        sub(/bc.game.team-b.*$/, "bc.game.team-b=#{opponent}")

    IO.write('bc.conf', custom)
    # system "ant file"
    `ant file`
    result = `zgrep winner match.rms | tail -n 1` 
    win = result.include?('winner="A"')
    puts "#{map}: #{us} vs #{opponent}: #{win ? "WON" : "LOST"}"
    win
end

us = 'optimo'
results = []
maps.each do |map|
    opponents.each do |opponent|
        results << play(map, us, opponent)
    end
end

