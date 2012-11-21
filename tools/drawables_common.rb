module Inkscape
  extend self
  
  INKSCAPE_PATH = "/Applications/Inkscape.app/Contents/Resources/bin/inkscape"

  def invoke(args)
    system "#{INKSCAPE_PATH} #{args}"
  end

  def export(svg_path, png_path, dpi)
    Inkscape.invoke "--file #{svg_path} --export-png #{png_path} --export-dpi #{dpi}"
  end
end

class DensityMap
  def self.default(mdpi_res)
    self.new(ldpi: mdpi_res * 120 / 160,
             mdpi: mdpi_res,
             hdpi: mdpi_res * 240 / 160,
             xhdpi: mdpi_res * 320 / 160)
  end
  
  def initialize(denisty_resolution_map = {})
    @map = denisty_resolution_map
  end

  def each_density(resources_dir, &block)
    @map.each_pair do |density, dpi|
      dest_dir = drawable_dir_for_density(resources_dir, density)
      block.call(dest_dir, dpi)
    end
  end

  def drawable_dir_for_density(basedir, density)
    basedir + ('drawable' + '-' + density.to_s)
  end
end

def png_path_from(svg_path, dest_dir)
  base_name = svg_path.basename(".*")
  dest_png = dest_dir + "#{base_name}.png"
end
